import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Reservation } from '../../models/models';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation.component.html',
  styles: []
})
export class ConfirmationComponent {
  @Input() reservation: Reservation | null = null;
  @Output() restart = new EventEmitter<void>();

  onRestart(): void {
    this.restart.emit();
  }
}
