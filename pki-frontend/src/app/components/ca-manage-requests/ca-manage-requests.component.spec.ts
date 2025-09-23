import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaManageRequestsComponent } from './ca-manage-requests.component';

describe('CaManageRequestsComponent', () => {
  let component: CaManageRequestsComponent;
  let fixture: ComponentFixture<CaManageRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaManageRequestsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaManageRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
