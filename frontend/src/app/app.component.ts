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
  template: `
    <main class="container pb-5">
      <!-- Error Alert -->
      <div *ngIf="errorMessage" class="error-alert animate-fade-in-up mb-4">
        <span>Error:</span>
        <span>{{ errorMessage }}</span>
        <button class="btn btn-sm btn-link text-danger ms-auto" (click)="clearError()">Dismiss</button>
      </div>

      <!-- Step 1: Method Selection (Always Visible) -->
      <div class="mb-5">
        <h2 class="h4 mb-4">Step 1: Select Your Delivery Method</h2>
        
        <div *ngIf="loadingMethods" class="text-center py-5">
          <div class="spinner mx-auto"></div>
          <p class="mt-3 text-muted">Loading delivery options...</p>
        </div>

        <div *ngIf="!loadingMethods" class="row justify-content-center">
          <div class="col-md-6 col-lg-4">
            <label class="form-label fw-semibold">Delivery Method</label>
            <select class="form-select form-select-lg" 
                    [(ngModel)]="selectedMethod"
                    (ngModelChange)="onMethodChange()"
                    [compareWith]="compareMethod">
              <option [ngValue]="null" disabled>-- Select a delivery method --</option>
              <option *ngFor="let method of deliveryMethods" [ngValue]="method">
                {{ method.name }}
              </option>
            </select>
            <div *ngIf="selectedMethod" class="text-muted small mt-2">
              {{ selectedMethod.description }}
            </div>
          </div>
        </div>
      </div>

      <!-- Step 2: Slot Selection (Visible when method selected) -->
      <div *ngIf="selectedMethod" class="mb-5">
        <h2 class="h4 mb-4">Step 2: Choose Your Time Slot</h2>
        <p class="text-muted mb-3">Selected method: {{ selectedMethod?.name }}</p>

        <!-- Date Picker (for methods that support date selection) -->
        <div *ngIf="!isAsapMethod()" class="mb-4">
          <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
              <label class="form-label fw-semibold">Select Date</label>
              <input type="date" 
                     class="form-control form-control-lg"
                     [value]="selectedDate"
                     [min]="minDate"
                     [max]="maxDate"
                     (change)="onDateChange($event)">
            </div>
          </div>
        </div>

        <!-- Slots -->
        <div *ngIf="loadingSlots" class="text-center py-5">
          <div class="spinner mx-auto"></div>
          <p class="mt-3 text-muted">Loading available slots...</p>
        </div>

        <div *ngIf="!loadingSlots && timeSlots.length > 0" class="row g-3 justify-content-center">
          <div *ngFor="let slot of timeSlots" class="col-md-6 col-lg-3">
            <div class="slot-card" 
                 [class.selected]="selectedSlot?.id === slot.id"
                 [class.disabled]="slot.status === 'RESERVED'"
                 (click)="selectSlot(slot)">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="fw-semibold">{{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}</div>
                  <div class="text-muted small">{{ formatDate(slot.date) }}</div>
                </div>
                <span [class]="slot.status === 'AVAILABLE' ? 'badge-available' : 'badge-reserved'">
                  {{ slot.status === 'AVAILABLE' ? 'Available' : 'Reserved' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div *ngIf="!loadingSlots && timeSlots.length === 0" class="text-center py-5">
          <p class="text-muted">No slots available for this date. Please try another date.</p>
        </div>

        <!-- Customer ID & Reserve Button -->
        <div *ngIf="selectedSlot" class="mt-4">
          <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
              <label class="form-label fw-semibold">Your Customer ID</label>
              <input type="text" 
                     class="form-control form-control-lg mb-3"
                     [(ngModel)]="customerId"
                     placeholder="Enter your customer ID">
            </div>
          </div>
          <div class="text-center">
            <button class="btn btn-primary-gradient btn-lg" 
                    [disabled]="!customerId || reserving"
                    (click)="makeReservation()">
              <span *ngIf="!reserving">Reserve This Slot</span>
              <span *ngIf="reserving">Reserving...</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Step 3: Confirmation (Visible when reservation made) -->
      <div *ngIf="reservation" class="mb-5">
        <div class="row justify-content-center">
          <div class="col-md-8 col-lg-6">
            <div class="confirmation-card">
              <div class="text-center mb-3">
                <h2 class="h3">Reservation Confirmed!</h2>
              </div>
              <p class="text-muted mb-4 text-center">Your delivery slot has been successfully reserved.</p>
              
              <div class="bg-light rounded-3 p-4 mb-4 text-start">
                <div class="row g-3">
                  <div class="col-6">
                    <div class="text-muted small">Reservation ID</div>
                    <div class="fw-semibold">#{{ reservation?.id }}</div>
                  </div>
                  <div class="col-6">
                    <div class="text-muted small">Delivery Method</div>
                    <div class="fw-semibold">{{ selectedMethod?.name }}</div>
                  </div>
                  <div class="col-6">
                    <div class="text-muted small">Date</div>
                    <div class="fw-semibold">{{ formatDate(reservation?.date || '') }}</div>
                  </div>
                  <div class="col-6">
                    <div class="text-muted small">Time Slot</div>
                    <div class="fw-semibold">{{ formatTime(reservation?.startTime || '') }} - {{ formatTime(reservation?.endTime || '') }}</div>
                  </div>
                  <div class="col-12">
                    <div class="text-muted small">Customer ID</div>
                    <div class="fw-semibold">{{ reservation?.customerId }}</div>
                  </div>
                </div>
              </div>

              <div class="text-center">
                <button class="btn btn-primary-gradient btn-lg" (click)="startOver()">
                  Make Another Reservation
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  `,
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
  customerId: string = '';
  reservation: Reservation | null = null;

  // Loading states
  loadingMethods = false;
  loadingSlots = false;
  reserving = false;

  // Error handling
  errorMessage = '';

  // Date constraints
  minDate: string = '';
  maxDate: string = '';

  constructor(private deliveryService: DeliveryService) { }

  ngOnInit(): void {
    this.loadDeliveryMethods();
    this.setDateConstraints();
  }

  private setDateConstraints(): void {
    const today = new Date();
    this.minDate = this.formatDateForInput(today);

    const maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 30);
    this.maxDate = this.formatDateForInput(maxDate);

    this.selectedDate = this.minDate;
  }

  private formatDateForInput(date: Date): string {
    return date.toISOString().split('T')[0];
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
    this.customerId = '';

    // For DELIVERY_TODAY and ASAP, force today's date
    if (this.selectedMethod.code === 'DELIVERY_TODAY' || this.selectedMethod.code === 'DELIVERY_ASAP') {
      this.selectedDate = this.minDate;
    }

    // Automatically load slots
    this.loadTimeSlots();
  }

  compareMethod(m1: DeliveryMethod | null, m2: DeliveryMethod | null): boolean {
    return m1?.code === m2?.code;
  }



  loadTimeSlots(): void {
    if (!this.selectedMethod) return;

    this.loadingSlots = true;
    this.selectedSlot = null;

    this.deliveryService.getTimeSlots(this.selectedMethod.code, this.selectedDate).subscribe({
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
    if (slot.status === 'RESERVED') return;
    this.selectedSlot = slot;
  }

  makeReservation(): void {
    if (!this.selectedMethod || !this.selectedSlot || !this.customerId) return;

    this.reserving = true;
    this.clearError();

    const request: ReservationRequest = {
      method: this.selectedMethod.code,
      date: this.selectedDate,
      slotId: this.selectedSlot.id,
      customerId: this.customerId
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
    this.customerId = '';
    this.reservation = null;
    this.timeSlots = [];
    this.setDateConstraints();
  }

  clearError(): void {
    this.errorMessage = '';
  }

  isAsapMethod(): boolean {
    return this.selectedMethod?.code === 'DELIVERY_ASAP';
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
