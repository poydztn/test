import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export type DeliveryMethod = string;

export interface TimeSlot {
    id: number;
    method: string;
    date: string;
    startTime: string;
    endTime: string;
}

export interface ReservationRequest {
    method: string;
    date: string;
    slotId: number;
}

export interface Reservation {
    id: number;
    slotId: number;
    method: string;
    date: string;
    startTime: string;
    endTime: string;
}

export interface ApiError {
    status: number;
    message: string;
}

@Injectable({
    providedIn: 'root'
})
export class DeliveryService {
    private apiUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    getDeliveryMethods(): Observable<DeliveryMethod[]> {
        return this.http.get<DeliveryMethod[]>(`${this.apiUrl}/delivery-methods`)
            .pipe(catchError(this.handleError));
    }

    getTimeSlots(method: string, date: string): Observable<TimeSlot[]> {
        return this.http.get<TimeSlot[]>(`${this.apiUrl}/time-slots`, {
            params: { method, date }
        }).pipe(catchError(this.handleError));
    }

    createReservation(request: ReservationRequest): Observable<Reservation> {
        return this.http.post<Reservation>(`${this.apiUrl}/reservations`, request)
            .pipe(catchError(this.handleError));
    }

    getReservation(id: number): Observable<Reservation> {
        return this.http.get<Reservation>(`${this.apiUrl}/reservations/${id}`)
            .pipe(catchError(this.handleError));
    }

    private handleError(error: HttpErrorResponse): Observable<never> {
        let apiError: ApiError = {
            status: error.status,
            message: 'An unexpected error occurred'
        };

        if (error.error && error.error.message) {
            apiError.message = error.error.message;
        } else if (error.status === 409) {
            apiError.message = 'This slot has already been reserved. Please choose another.';
        } else if (error.status === 400) {
            apiError.message = 'Invalid request. Please check your selection.';
        }

        return throwError(() => apiError);
    }
}
