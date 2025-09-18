import { provideHttpClient } from "@angular/common/http";
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { AppComponent } from "./app/app.component";
import { bootstrapApplication } from "@angular/platform-browser";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { importProvidersFrom } from "@angular/core";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(), provideAnimationsAsync(),
    importProvidersFrom(MatSnackBarModule)
  ]
}).catch((err) =>
  console.error(err)
);
