import { Component } from '@angular/core';
import { StockSearch } from '../stocks/stock-search/stock-search';

@Component({
  imports: [StockSearch],
  selector: 'app-home',
  styleUrl: './home.scss',
  templateUrl: './home.html',
})
export class Home {}
