import { Routes } from '@angular/router';
import { WelcomePageComponent } from './components/welcome-page/welcome-page.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { provideHttpClient } from '@angular/common/http';
import { AdminHomeComponent } from './components/admin-home/admin-home.component';
import { CreateRootCertificateComponent } from './components/create-root-certificate/create-root-certificate.component';
import { CaHomeComponent } from './components/ca-home/ca-home.component';

export const routes: Routes = [
    { path: '', component: WelcomePageComponent },
    { path: 'login', component: LoginComponent },
    { 
    path: 'register', 
    component: RegisterComponent,
    providers: [provideHttpClient()]  
    },
    { path: 'admin-home', component: AdminHomeComponent },
    { path: 'admin/create-root', component: CreateRootCertificateComponent },
    { path: 'ca-home', component: CaHomeComponent},
];
