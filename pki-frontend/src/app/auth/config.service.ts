import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private _api_url = 'https://localhost:8443/api';

  private _login_url = this._api_url + '/users/login';

  get login_url(): string {
    return this._login_url;
  }

  private _current_user_url = this._api_url + '/users/currentUser';

  get current_user_url(): string {
    return this._current_user_url;
  }
  get api_url(): string{
    return this._api_url;
  }

}
