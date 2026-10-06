// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import {BrowserAnimationsModule, provideAnimations} from '@angular/platform-browser/animations';
import {FournisseurService} from './app/services/fournisseur.service';
import {AuthService} from './app/services/auth.service';
import {importProvidersFrom} from '@angular/core';
import {ToastrModule} from 'ngx-toastr';
import {tokenInterceptor} from './app/services/token.interceptor';


bootstrapApplication(AppComponent, {
  providers: [
    provideHttpClient(
      withInterceptors([tokenInterceptor])
    ),  // OBLIGATOIRE !!
    provideRouter(routes),
    provideAnimations(),
    AuthService ,
    importProvidersFrom(
      BrowserAnimationsModule,
      ToastrModule.forRoot({
        positionClass: 'toast-bottom-right',
        timeOut: 3000,
        preventDuplicates: true,
      })
    )
  ]
}).catch(err => console.error(err));
