import { Routes } from '@angular/router';
import { WelcomePageComponent } from './components/welcome-page/welcome-page.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';

export const routes: Routes = [
    { path: '', component: WelcomePageComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
];
