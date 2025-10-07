import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserCreateEndCertificateComponent } from './user-create-end-certificate.component';

describe('UserCreateEndCertificateComponent', () => {
  let component: UserCreateEndCertificateComponent;
  let fixture: ComponentFixture<UserCreateEndCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserCreateEndCertificateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserCreateEndCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
