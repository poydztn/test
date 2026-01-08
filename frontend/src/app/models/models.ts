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
