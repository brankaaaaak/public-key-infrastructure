import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { AppComponent } from "./app/app.component";
import { bootstrapApplication } from "@angular/platform-browser";
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { importProvidersFrom } from "@angular/core";
import { Interceptor } from "./app/auth/interceptor";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(), provideAnimationsAsync(),
    importProvidersFrom(MatSnackBarModule),
    provideHttpClient(
        withInterceptorsFromDi(),
    ),
    {
        provide: HTTP_INTERCEPTORS,
        useClass: Interceptor,
        multi: true,
    },
  ]
}).catch((err) =>
  console.error(err)
);
