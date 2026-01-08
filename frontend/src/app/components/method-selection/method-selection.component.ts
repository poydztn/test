import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-method-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './method-selection.component.html',
  styles: []
})
export class MethodSelectionComponent {
  @Input() methods: string[] = [];
  @Input() loading = false;
  @Input() selectedMethod: string | null = null;
  @Output() selectedMethodChange = new EventEmitter<string | null>();

  onMethodChange(): void {
    this.selectedMethodChange.emit(this.selectedMethod);
  }
}
