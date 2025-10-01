import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaViewCertificateChainComponent } from './ca-view-certificate-chain.component';

describe('CaViewCertificateChainComponent', () => {
  let component: CaViewCertificateChainComponent;
  let fixture: ComponentFixture<CaViewCertificateChainComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaViewCertificateChainComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaViewCertificateChainComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
