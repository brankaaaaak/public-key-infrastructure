import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaCreateIntermediateCertificateComponent } from './ca-create-intermediate-certificate.component';

describe('CaCreateIntermediateCertificateComponent', () => {
  let component: CaCreateIntermediateCertificateComponent;
  let fixture: ComponentFixture<CaCreateIntermediateCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaCreateIntermediateCertificateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaCreateIntermediateCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
