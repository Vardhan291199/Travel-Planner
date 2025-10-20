import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, AutocompleteItem, PlaceItem, CountryInfo, ConvertResult } from './api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  private api = inject(ApiService);

  query = '';
  suggestions: AutocompleteItem[] = [];
  showDropdown = false;
  selectedCity?: AutocompleteItem;

  restaurants: PlaceItem[] = [];
  hotels: PlaceItem[] = [];
  country?: CountryInfo;
  fxFrom = 'USD';
  fxTo = 'USD';
  fxAmount = 100;
  fx?: ConvertResult;
  code = 'US';

  private timer?: any;
  onQuery(q: string) {
    this.query = q;
    if (this.timer) clearTimeout(this.timer);
    if (!q || q.length < 2) { this.suggestions = []; this.showDropdown = false; return; }
    this.timer = setTimeout(() => {
      this.api.autocomplete(q, undefined).subscribe(items => {
        this.suggestions = items;
        this.showDropdown = items.length > 0;
      });
    }, 250);
  }

  pickSuggestion(item: AutocompleteItem) {
    this.selectedCity = item;
    this.query = item.label ?? '';
    this.showDropdown = false;
    this.code = (item.countryCode || '').toUpperCase();
    this.loadAll();
    this.loadCountry();
  }

  loadAll() {
    if (!this.selectedCity) return;
    const { lat, lon, countryCode } = this.selectedCity;
    this.api.places(lat!, lon!, 3000, 'restaurant').subscribe(v => this.restaurants = v);
    this.api.places(lat!, lon!, 3000, 'hotel').subscribe(v => this.hotels = v);
  }

  loadCountry() {
    const c = (this.code || '').trim().toUpperCase();
    if (!c) { this.country = undefined; return; }
    this.api.country(c).subscribe({
      next: v => {this.country = v;
      this.fxFrom = this.country?.currency ?? 'USD';},
      error: err => console.error('country error', err)
  });
}

  convert() {
    this.api.convert(this.fxFrom, this.fxTo, this.fxAmount).subscribe(v => this.fx = v);
  }

  fmtDist(m?: number) { return m ? `${m.toFixed(0)} m` : ''; }
}
