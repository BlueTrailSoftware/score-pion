import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { InputComponent } from '../../../app/components/input/input.component';

describe('InputComponent', () => {
  let component: InputComponent;
  let fixture: ComponentFixture<InputComponent>;
  let parentForm: FormGroup;
  let control: FormControl;

  beforeEach(async () => {
    control = new FormControl('');
    parentForm = new FormGroup({ testField: control });

    await TestBed.configureTestingModule({
      imports: [InputComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;

    // Set required inputs
    component.type = 'text';
    component.id = 'test-input';
    component.name = 'testField';
    component.labelText = 'Test Label';
    component.parentForm = parentForm;
    component.control = control;
    component.validators = [];
    component.errors = {};

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('setValidators', () => {
    it('should apply validators to the control', () => {
      component.validators = [Validators.required];
      component.setValidators();

      control.setValue('');
      expect(control.hasError('required')).toBeTrue();
    });

    it('should clear previous validators before setting new ones', () => {
      component.validators = [Validators.required];
      component.setValidators();

      component.validators = [];
      component.setValidators();

      control.setValue('');
      expect(control.valid).toBeTrue();
    });
  });

  describe('checkForErrors', () => {
    it('should set displayError to true when control has errors', fakeAsync(() => {
      component.validators = [Validators.required];
      component.setValidators();
      control.setValue('');

      component.checkForErrors(false);
      tick(200);

      expect(component.displayError).toBeTrue();
    }));

    it('should set displayError to false when control has no errors', fakeAsync(() => {
      component.validators = [Validators.required];
      component.setValidators();
      control.setValue('valid');

      component.checkForErrors(false);
      tick(200);

      expect(component.displayError).toBeFalse();
    }));

    it('should emit onBlur when not a validator update', () => {
      spyOn(component.onBlur, 'emit');
      component.checkForErrors(false);
      expect(component.onBlur.emit).toHaveBeenCalledWith('blur');
    });

    it('should NOT emit onBlur when it is a validator update', () => {
      spyOn(component.onBlur, 'emit');
      component.checkForErrors(true);
      expect(component.onBlur.emit).not.toHaveBeenCalled();
    });

    it('should always emit blurred', () => {
      spyOn(component.blurred, 'emit');
      component.checkForErrors(true);
      expect(component.blurred.emit).toHaveBeenCalledWith('blurred');
    });
  });

  describe('disableSpace', () => {
    it('should prevent default when shouldDisableSpace is true', () => {
      component.shouldDisableSpace = true;
      const event = new KeyboardEvent('keydown', { key: ' ' });
      spyOn(event, 'preventDefault');

      component.disableSpace(event);

      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('should not prevent default when shouldDisableSpace is false', () => {
      component.shouldDisableSpace = false;
      const event = new KeyboardEvent('keydown', { key: ' ' });
      spyOn(event, 'preventDefault');

      component.disableSpace(event);

      expect(event.preventDefault).not.toHaveBeenCalled();
    });
  });

  describe('trimElement', () => {
    it('should trim whitespace and update control value', () => {
      const input = document.createElement('input');
      input.value = '  hello  ';

      component.trimElement(input);

      expect(input.value).toBe('hello');
      expect(control.value).toBe('hello');
    });

    it('should do nothing when element is null', () => {
      const originalValue = control.value;
      component.trimElement(null);
      expect(control.value).toBe(originalValue);
    });
  });

  describe('onInput', () => {
    it('should emit whenInput and onInputEvent', () => {
      spyOn(component.whenInput, 'emit');
      spyOn(component.onInputEvent, 'emit');
      const event = new Event('input');

      component.onInput(event);

      expect(component.whenInput.emit).toHaveBeenCalledWith('input');
      expect(component.onInputEvent.emit).toHaveBeenCalledWith(event);
    });

    it('should call checkForErrors when isDatabaseInput is true', () => {
      component.isDatabaseInput = true;
      spyOn(component, 'checkForErrors');

      component.onInput(new Event('input'));

      expect(component.checkForErrors).toHaveBeenCalledWith(true);
    });
  });

  describe('checkTypeForPadding', () => {
    it('should set showIcon to false when type is not password', () => {
      component.showIcon = true;
      component.type = 'text';
      component.checkTypeForPadding();
      expect(component.showIcon).toBeFalse();
    });

    it('should keep showIcon unchanged when type is password', () => {
      component.showIcon = true;
      component.type = 'password';
      component.checkTypeForPadding();
      expect(component.showIcon).toBeTrue();
    });
  });

  describe('focused', () => {
    it('should emit onFocus event', () => {
      spyOn(component.onFocus, 'emit');
      component.focused();
      expect(component.onFocus.emit).toHaveBeenCalledWith('focus');
    });
  });

  describe('pressDelete', () => {
    it('should emit onDelete event', () => {
      spyOn(component.onDelete, 'emit');
      component.pressDelete();
      expect(component.onDelete.emit).toHaveBeenCalledWith('delete');
    });
  });

  describe('focusInput', () => {
    it('should focus the native input element', () => {
      spyOn(component.inputField.nativeElement, 'focus');
      component.focusInput();
      expect(component.inputField.nativeElement.focus).toHaveBeenCalled();
    });

    it('should not focus when isFromOldForm is true', () => {
      component.isFromOldForm = true;
      spyOn(component.inputField.nativeElement, 'focus');
      component.focusInput();
      expect(component.inputField.nativeElement.focus).not.toHaveBeenCalled();
    });
  });

  describe('selectInput', () => {
    it('should select the native input element', () => {
      spyOn(component.inputField.nativeElement, 'select');
      component.selectInput();
      expect(component.inputField.nativeElement.select).toHaveBeenCalled();
    });

    it('should not select when isFromOldForm is true', () => {
      component.isFromOldForm = true;
      spyOn(component.inputField.nativeElement, 'select');
      component.selectInput();
      expect(component.inputField.nativeElement.select).not.toHaveBeenCalled();
    });
  });
});
