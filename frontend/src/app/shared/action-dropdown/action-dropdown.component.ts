import { Component, Input, HostListener, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, Subscription } from 'rxjs';

export interface ActionItem {
  label: string;
  icon: string;
  colorClass: string;
  action: () => void;
  dividerBefore?: boolean;
  disabled?: boolean;
}

@Component({
  selector: 'app-action-dropdown',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './action-dropdown.component.html',
  styleUrls: ['./action-dropdown.component.scss'],
})
export class ActionDropdownComponent implements OnDestroy {
  @Input() actions: ActionItem[] = [];
  @Input() disabled = false;
  @Input() type: 'icon' | 'gradient' = 'icon';
  @Input() label = 'Actions';

  isOpen = false;
  menuStyle: { top: string; right: string } = { top: '0px', right: '0px' };

  private static closeAll$ = new Subject<ActionDropdownComponent>();
  private sub: Subscription;

  constructor() {
    this.sub = ActionDropdownComponent.closeAll$.subscribe(source => {
      if (source !== this) this.isOpen = false;
    });
  }

  toggle(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.isOpen) {
      const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
      this.menuStyle = {
        top: `${rect.bottom + 2}px`,
        // Calculate distance from right edge of window to align right edge of box with right edge of button
        right: `${window.innerWidth - rect.right}px`,
      };
      ActionDropdownComponent.closeAll$.next(this);
    }
    this.isOpen = !this.isOpen;
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.isOpen = false;
  }

  runAction(item: ActionItem, event: MouseEvent): void {
    event.stopPropagation();
    if (item.disabled) return;

    this.isOpen = false;
    item.action();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
