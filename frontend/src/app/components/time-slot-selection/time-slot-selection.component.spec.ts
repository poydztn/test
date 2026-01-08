import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimeSlotSelectionComponent } from './time-slot-selection.component';

describe('TimeSlotSelectionComponent', () => {
  let component: TimeSlotSelectionComponent;
  let fixture: ComponentFixture<TimeSlotSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimeSlotSelectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimeSlotSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
