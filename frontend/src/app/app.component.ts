import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  DeliveryService,
  DeliveryMethod,
  TimeSlot,
  Reservation,
  ReservationRequest,
  ApiError
} from './services/delivery.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styles: []
})
export class AppComponent implements OnInit {
  // Data
  deliveryMethods: DeliveryMethod[] = [];
  timeSlots: TimeSlot[] = [];

  // Selections
  selectedMethod: DeliveryMethod | null = null;
  selectedDate: string = '';
  selectedSlot: TimeSlot | null = null;
  reservation: Reservation | null = null;

  // Loading states
  loadingMethods = false;
  loadingSlots = false;
  reserving = false;

  // Error handling
  errorMessage = '';



  constructor(private deliveryService: DeliveryService) { }

  ngOnInit(): void {
    this.loadDeliveryMethods();
    this.selectedDate = new Date().toISOString().split('T')[0];
  }



  loadDeliveryMethods(): void {
    this.loadingMethods = true;
    this.deliveryService.getDeliveryMethods().subscribe({
      next: (methods) => {
        this.deliveryMethods = methods;
        this.loadingMethods = false;
      },
      error: (err: ApiError) => {
        this.errorMessage = err.message;
        this.loadingMethods = false;
      }
    });
  }

  onMethodChange(): void {
    if (!this.selectedMethod) return;

    // Reset slot selection when method changes
    this.selectedSlot = null;
    this.timeSlots = [];

    // For DELIVERY_TODAY and ASAP, force today's date


    // Automatically load slots
    this.loadTimeSlots();
  }





  loadTimeSlots(): void {
    if (!this.selectedMethod) return;

    this.loadingSlots = true;
    this.selectedSlot = null;

    this.deliveryService.getTimeSlots(this.selectedMethod, this.selectedDate).subscribe({
      next: (slots) => {
        this.timeSlots = slots;
        this.loadingSlots = false;
      },
      error: (err: ApiError) => {
        this.errorMessage = err.message;
        this.loadingSlots = false;
      }
    });
  }

  onDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedDate = input.value;
    this.loadTimeSlots();
  }

  selectSlot(slot: TimeSlot): void {
    this.selectedSlot = slot;
  }

  makeReservation(): void {
    if (!this.selectedMethod || !this.selectedSlot) return;

    this.reserving = true;
    this.clearError();

    const request: ReservationRequest = {
      method: this.selectedMethod,
      date: this.selectedDate,
      slotId: this.selectedSlot.id
    };

    this.deliveryService.createReservation(request).subscribe({
      next: (reservation) => {
        this.reservation = reservation;
        this.reserving = false;
      },
      error: (err: ApiError) => {
        this.errorMessage = err.message;
        this.reserving = false;
        // Reload slots to get updated availability
        this.loadTimeSlots();
      }
    });
  }



  startOver(): void {
    this.selectedMethod = null;
    this.selectedSlot = null;
    this.reservation = null;
    this.timeSlots = [];

  }

  clearError(): void {
    this.errorMessage = '';
  }

  isAsapMethod(): boolean {
    return this.selectedMethod === 'DELIVERY_ASAP';
  }



  formatTime(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    return `${hours}:${minutes}`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric'
    });
  }
}
