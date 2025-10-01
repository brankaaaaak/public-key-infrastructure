import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCreateIntermediateCertificateComponent } from './admin-create-intermediate-certificate.component';

describe('AdminCreateIntermediateCertificateComponent', () => {
  let component: AdminCreateIntermediateCertificateComponent;
  let fixture: ComponentFixture<AdminCreateIntermediateCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCreateIntermediateCertificateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminCreateIntermediateCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
