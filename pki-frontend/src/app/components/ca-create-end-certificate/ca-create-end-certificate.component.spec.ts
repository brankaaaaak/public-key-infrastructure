import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaCreateEndCertificateComponent } from './ca-create-end-certificate.component';

describe('CaCreateEndCertificateComponent', () => {
  let component: CaCreateEndCertificateComponent;
  let fixture: ComponentFixture<CaCreateEndCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaCreateEndCertificateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaCreateEndCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
