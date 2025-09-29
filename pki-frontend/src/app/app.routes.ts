import { Routes } from '@angular/router';
import { WelcomePageComponent } from './components/welcome-page/welcome-page.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { provideHttpClient } from '@angular/common/http';
import { AdminHomeComponent } from './components/admin-home/admin-home.component';
import { CreateRootCertificateComponent } from './components/create-root-certificate/create-root-certificate.component';
import { CaHomeComponent } from './components/ca-home/ca-home.component';
import { CaCreateIntermediateCertificateComponent } from './components/ca-create-intermediate-certificate/ca-create-intermediate-certificate.component';
import { CaCreateEndCertificateComponent } from './components/ca-create-end-certificate/ca-create-end-certificate.component';
import { UserCreateEndCertificateComponent } from './components/user-create-end-certificate/user-create-end-certificate.component';
import { UserHomeComponent } from './components/user-home/user-home.component';
import { AdminManageRequestsComponent } from './components/admin-manage-requests/admin-manage-requests.component';
import { provideAnimations } from '@angular/platform-browser/animations';
import { CaManageRequestsComponent } from './components/ca-manage-requests/ca-manage-requests.component';
import { UserDownloadComponent } from './components/user-download/user-download.component';

export const routes: Routes = [
    { path: '', component: WelcomePageComponent },
    { path: 'login', component: LoginComponent },
    { 
    path: 'register', 
    component: RegisterComponent,
    providers: [provideHttpClient(),provideAnimations()]  
    },
    { path: 'admin-home', component: AdminHomeComponent },
    { path: 'admin/create-root', component: CreateRootCertificateComponent },
    { path: 'ca-home', component: CaHomeComponent},
    { path: 'ca/ca-create-intermediate-certificate', component: CaCreateIntermediateCertificateComponent},
    { path: 'ca/ca-create-end-certificate', component: CaCreateEndCertificateComponent},
    { path: 'user-home', component: UserHomeComponent},
    { path: 'user/user-create-end-certificate', component: UserCreateEndCertificateComponent},
    { path: 'admin/requests', component: AdminManageRequestsComponent},
    { path: 'ca/requests', component: CaManageRequestsComponent},
    { path: 'user/download', component: UserDownloadComponent},
];
