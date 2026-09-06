import { Component } from '@angular/core';
import { Search } from '../search/search';

@Component({
  imports: [Search],
  selector: 'app-home',
  styleUrl: './home.scss',
  templateUrl: './home.html',
})
export class Home {}
