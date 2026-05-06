import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { FormControl, FormGroup, ValidatorFn } from '@angular/forms';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './input.component.html',
  styleUrl: './input.component.scss',
})
export class InputComponent implements OnInit, OnChanges, AfterViewInit {
  private detectorRef = inject(ChangeDetectorRef);

  @ViewChild('inputField', { static: false }) inputField!: ElementRef;

  @Input() type!: string;
  @Input() id!: string;
  @Input() name!: string;
  @Input() labelText!: string;
  @Input() parentForm!: FormGroup;
  @Input() control!: FormControl;
  @Input() validators!: ValidatorFn[];
  @Input() errors!: { [key: string]: string };
  @Input() shouldDisableSpace: boolean = false;
  @Input() withImage: boolean = false;
  @Input() showIcon: boolean = false;
  @Input() min!: number;
  @Input() max!: number;
  @Input() relativeErrors: boolean = false;
  @Input() relativeMobileErrors: boolean = false;
  @Input() capitalize: boolean = false;
  @Input() showLabel: boolean = true;
  @Input() centerText: boolean = false;
  @Input() focus: boolean = false;
  @Input() dependecy: boolean = false;
  @Input() isDatabaseInput?: boolean;
  @Input() isFromOldForm?: boolean;
  @Input() showDeleteButton?: boolean;

  @Output() onBlur = new EventEmitter();
  @Output() whenInput: EventEmitter<string> = new EventEmitter<string>();
  @Output() onInputEvent: EventEmitter<Event> = new EventEmitter<Event>();
  @Output() blurred: EventEmitter<string> = new EventEmitter<string>();
  @Output() onFocus: EventEmitter<string> = new EventEmitter<string>();
  @Output() onDelete: EventEmitter<string> = new EventEmitter<string>();

  public errorKeys!: string[];
  public errorMessages!: string[];
  public displayError: boolean = false;

  /**
   * ngOnChanges hook
   * detects changes in input properties
   * and set focus on input if focus variable
   * @param {SimpleChanges} changes object containing the
   * changes of input properties
   */
  ngOnChanges(changes: SimpleChanges) {
    this.control.clearValidators();
    this.setValidators();

    if (changes['validators']) {
      if (!changes['validators'].isFirstChange()) {
        this.checkForErrors(true);
      }
    }

    if (this.focus && this.inputField) {
      this.focusInput();
    }
  }

  /**
   * ngOnInit hook
   * set the validators at the start of the
   * component
   */
  ngOnInit() {
    // set validators
    this.setValidators();
    this.checkTypeForPadding();
  }

  /**
   * ngAfterViewInit hook
   * focus the input after the field has been rendered
   */
  ngAfterViewInit() {
    if (this.focus) {
      this.focusInput();
      this.selectInput();
    }
  }

  /**
   * setValidators method
   * sets the validators for the form control
   * of this component and updates it to be
   * retroactive, emitting an event at the same
   * time
   * @returns {void}
   */
  public setValidators(): void {
    this.control.setValidators(this.validators);

    // make the validation retroactive
    this.control.updateValueAndValidity();
  }

  /**
   * checkForErrors method
   * set displayError flag to true if there are errors
   * @param {boolean} isValidatorUpdate is true if there are first
   * changes in validators
   * @returns {void}
   */
  public checkForErrors(isValidatorUpdate: boolean): void {
    if (!isValidatorUpdate) {
      this.onBlur.emit('blur');
    }

    setTimeout(() => {
      if (this.control.errors) {
        this.displayError = true;
      } else {
        this.displayError = false;
      }
    }, 150);

    this.blurred.emit('blurred');
  }

  /**
   * disableSpace method
   * disable the spacebar for certain inputs
   * if specified
   * @returns {void}
   */
  public disableSpace(event: Event): void {
    if (this.shouldDisableSpace && event instanceof KeyboardEvent) {
      event.returnValue = false;
      event.preventDefault();
    }
  }

  /**
   * trimElement method
   * trims the value of the element
   * out of excessive whitespace
   * @param {HTMLInputElement} element element to trim
   * @returns {void}
   */
  public trimElement(element: EventTarget | null): void {
    if (element && element instanceof HTMLInputElement) {
      element.value = element.value.trim();
      this.control.setValue(element.value);
    }
  }

  /**
   * onInput method
   * send event emiter to father
   * @param {Event} event event for the input
   * @param {string} value previous value of the control
   * @return {void}
   * */
  public onInput(event: Event): void {
    if (this.isDatabaseInput) {
      this.checkForErrors(true);
    }

    this.whenInput.emit('input');
    this.onInputEvent.emit(event);
  }

  /**
   * focusInput method
   * once the user clicks the label, the
   * input should be focused
   * @return {void}
   */
  public focusInput(): void {
    if (!this.isFromOldForm) {
      this.inputField.nativeElement.focus();
      this.detectorRef.detectChanges();
    }
  }

  /**
   * checkTypeForPadding
   * checks when the input type is different to
   * password to add correct padding for the input
   * @return {void} void
   */
  public checkTypeForPadding(): void {
    if (this.type !== 'password') {
      this.showIcon = false;
    }
  }

  /**
   * focused method
   * @description If the input is in focus
   * emits an event to parent component to know it
   * @returns {void} void
   */
  public focused(): void {
    this.onFocus.emit('focus');
  }

  /**
   * pressDelete method
   * @description Emit an event to parent component
   * when the delete button was clicked
   * @returns {void} void
   */
  public pressDelete(): void {
    this.onDelete.emit('delete');
  }

  /**
   * selectInput method
   * @description Once the view init, the input should be selected
   * @returns {void} void
   */
  public selectInput(): void {
    if (!this.isFromOldForm) {
      // select all the content of the input
      this.inputField.nativeElement.select();
    }
  }
}
