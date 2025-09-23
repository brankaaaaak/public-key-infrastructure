import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminManageRequestsComponent } from './admin-manage-requests.component';

describe('AdminManageRequestsComponent', () => {
  let component: AdminManageRequestsComponent;
  let fixture: ComponentFixture<AdminManageRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminManageRequestsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminManageRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
