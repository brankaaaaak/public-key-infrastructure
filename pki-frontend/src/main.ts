import { provideHttpClient } from "@angular/common/http";
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import { AppComponent } from "./app/app.component";
import { bootstrapApplication } from "@angular/platform-browser";

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient()
  ]
}).catch((err) =>
  console.error(err)
);
