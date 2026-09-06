import { Component } from '@angular/core';
import { MatFormField, MatInput, MatLabel } from '@angular/material/input';

@Component({
  imports: [MatFormField, MatLabel, MatInput],
  selector: 'app-search',
  styleUrl: './search.scss',
  templateUrl: './search.html',
})
export class Search {}
