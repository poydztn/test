import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TimeSlot } from '../../models/models';

@Component({
  selector: 'app-time-slot-selection',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './time-slot-selection.component.html',
  styles: []
})
export class TimeSlotSelectionComponent {
  @Input() selectedMethod: string = '';
  @Input() timeSlots: TimeSlot[] = [];
  @Input() selectedDate: string = '';


  @Output() dateChange = new EventEmitter<string>();
  @Output() reserveSlot = new EventEmitter<TimeSlot>();

  // Internal selection state
  selectedSlot: TimeSlot | null = null;

  onDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.dateChange.emit(input.value);
    this.selectedSlot = null; // Reset selection on date change
  }

  selectSlot(slot: TimeSlot): void {
    this.selectedSlot = slot;
  }

  onReserve(): void {
    if (this.selectedSlot) {
      this.reserveSlot.emit(this.selectedSlot);
    }
  }

  formatTime(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    return `${hours}:${minutes}`;
  }


}
