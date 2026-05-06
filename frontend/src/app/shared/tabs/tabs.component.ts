import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface TabOption {
  id: string;
  label: string;
  count?: number;
}

/**
 * Standard Tabs component for Score-Pion Design System.
 * Uses the bottom-border design as the primary navigation pattern.
 */
@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tabs.component.html',
  styleUrls: ['./tabs.component.scss'],
})
export class TabsComponent {
  @Input() options: TabOption[] = [];
  @Input() activeId: string = '';
  @Output() tabChange = new EventEmitter<string>();

  selectTab(id: string): void {
    if (this.activeId !== id) {
      this.activeId = id;
      this.tabChange.emit(id);
    }
  }
}
