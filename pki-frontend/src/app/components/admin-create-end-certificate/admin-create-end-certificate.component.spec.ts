import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCreateEndCertificateComponent } from './admin-create-end-certificate.component';

describe('AdminCreateEndCertificateComponent', () => {
  let component: AdminCreateEndCertificateComponent;
  let fixture: ComponentFixture<AdminCreateEndCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCreateEndCertificateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminCreateEndCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
