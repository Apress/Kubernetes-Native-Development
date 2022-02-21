import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScraperconfigComponent } from './scraperconfig.component';

describe('ScraperconfigComponent', () => {
  let component: ScraperconfigComponent;
  let fixture: ComponentFixture<ScraperconfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScraperconfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScraperconfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
