import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.scss']
})
export class PaginationComponent implements OnChanges {
  @Input() currentPage: number = 1;
  @Input() totalItems: number = 0;
  @Input() pageSize: number = 10;
  @Input() disabled: boolean = false;

  @Output() pageChange = new EventEmitter<number>();

  totalPages: number = 0;
  pages: number[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['totalItems'] || changes['pageSize']) {
      this.calculateTotalPages();
    }
  }

  private calculateTotalPages(): void {
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    // Generate simple array first, simple optimization
    this.pages = Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  onPageChange(page: number): void {
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage && !this.disabled) {
      this.pageChange.emit(page);
    }
  }

  shouldShowPage(page: number): boolean {
    // Show first, last, current, and neighbors
    return page === 1 || 
           page === this.totalPages || 
           Math.abs(page - this.currentPage) <= 1;
  }

  shouldShowEllipsis(page: number): boolean {
    // Show ellipsis if this page is hidden but its predecessor wasn't the previous number
    // Actually, simplifying: we only render items in the template loop if they match criteria.
    // But to handle ellipsis correctly in *ngFor without complex logic, it's easier to generated a "view model" of pages.
    // Let's stick to the template logic I had earlier which was:
    // if (page === currentPage || page === 1 || page === totalPages || (page >= currentPage - 1 && page <= currentPage + 1))
    // else if (page === currentPage - 2 || page === currentPage + 2) show ...
    
    return page === this.currentPage - 2 || page === this.currentPage + 2;
  }
}
