import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AutocompleteItem {
  label?: string;
  city?: string;
  countryCode?: string;
  lat?: number;
  lon?: number;
}

export interface PlaceItem {
  name?: string;
  address?: string;
  distanceMeters?: number;
  categories?: string[];
  lat?: number;
  lon?: number;
}

export interface CountryInfo {
  name?: string;
  emoji?: string;
  capital?: string;
  currency?: string;
  continent?: string;
  languages?: string[];
}

export interface ConvertResult {
  rate?: number;
  result?: number;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = '/api';
  constructor(private http: HttpClient) {}

  autocomplete(text: string, limit: number = 8): Observable<AutocompleteItem[]> {
    const params: any = { text, limit };
    return this.http.get<AutocompleteItem[]>(`${this.base}/autocomplete`, { params });
  }

  places(lat: number, lon: number, radius = 3000, type: 'restaurant' | 'hotel' = 'restaurant'): Observable<PlaceItem[]> {
    return this.http.get<PlaceItem[]>(`${this.base}/places`, { params: { lat, lon, radius, type } });
    }

  country(code: string) {
  const params: any = { code };
  return this.http.get<{
    code: string; name: string; emoji: string; capital: string;
    currency: string; continent: string; languages: string[];
    }>(`${this.base}/country`, { params });
  }

  convert(from: string, to: string, amount: number) {
  return this.http.get<ConvertResult>(`${this.base}/fx/convert`, { params: { from, to, amount } });
}

}
