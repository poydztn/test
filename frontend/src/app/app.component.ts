import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MethodSelectionComponent } from './components/method-selection/method-selection.component';
import { TimeSlotSelectionComponent } from './components/time-slot-selection/time-slot-selection.component';
import { ConfirmationComponent } from './components/confirmation/confirmation.component';
import {
  DeliveryService
} from './services/delivery.service';
import {
  DeliveryMethod,
  TimeSlot,
  Reservation,
  ReservationRequest,
  ApiError
} from './models/models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, MethodSelectionComponent, TimeSlotSelectionComponent, ConfirmationComponent],
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

  onMethodSelect(method: string | null): void {
    this.selectedMethod = method;
    this.onMethodChange();
  }

  onMethodChange(): void {
    if (!this.selectedMethod) return;

    // Reset slot selection when method changes
    this.selectedSlot = null;
    this.timeSlots = [];

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

  onDateSelect(date: string): void {
    this.selectedDate = date;
    this.loadTimeSlots();
  }

  onReserveSlot(slot: TimeSlot): void {
    if (!this.selectedMethod) return;

    this.clearError();

    const request: ReservationRequest = {
      method: this.selectedMethod,
      date: this.selectedDate,
      slotId: slot.id
    };

    this.deliveryService.createReservation(request).subscribe({
      next: (reservation) => {
        this.reservation = reservation;
      },
      error: (err: ApiError) => {
        this.errorMessage = err.message;
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
}
