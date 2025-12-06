import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Trade {
  timestamp: string;
  symbol: string;
  side: string;
  quantity: string;
  price: number;
  usdt: number;
  btc: number;
}

export interface StrategyConfig {
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class TradeService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) { }

  getTrades(): Observable<Trade[]> {
    return this.http.get<Trade[]>(`${this.baseUrl}/trades`);
  }

  getStrategy(): Observable<StrategyConfig> {
    return this.http.get<StrategyConfig>(`${this.baseUrl}/strategy`);
  }

  updateStrategy(strategy: string, decorator: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/strategy`, { strategy, decorator });
  }
}
