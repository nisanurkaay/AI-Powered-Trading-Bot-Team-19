import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TradeService, Trade } from './services/trade.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('AI Trading Bot Dashboard');
  trades: Trade[] = [];
  connected = signal(true);
  
  currentStrategy = signal('Loading...');

  // Form State
  strategies = ['SmaCrossover', 'TrendFollowing'];
  decorators = ['None', 'CrashProtection', 'HighRisk', 'LowRisk'];
  
  selectedStrategy = 'SmaCrossover';
  selectedDecorator = 'None';

  // Portfolio Metrics
  portfolioValue = signal(0);
  usdtBalance = signal(0);
  btcBalance = signal(0);

  constructor(private tradeService: TradeService) {}

  ngOnInit() {
    this.fetchTrades();
    this.fetchStrategy();
    setInterval(() => this.fetchTrades(), 2000);
    setInterval(() => this.fetchStrategy(), 5000);
  }

  fetchTrades() {
    this.tradeService.getTrades().subscribe({
      next: (data) => {
        // Filter out HOLDs for the list, but use the latest data point for balance
        // We might need to look at raw data for the latest state even if it's a HOLD, 
        // but current API filters server side? No, API returns all.
        // Wait, app logic filtered holds.
        
        if (data.length > 0) {
            const latest = data[data.length - 1];
            this.usdtBalance.set(latest.usdt || 1000); // Default if missing
            this.btcBalance.set(latest.btc || 0);
            
            // Est Value = USDT + (BTC * Current Price)
            const price = latest.price || 0;
            this.portfolioValue.set(this.usdtBalance() + (this.btcBalance() * price));
        }

        this.trades = data.filter(t => t.side !== 'HOLD').reverse();
        this.connected.set(true);
      },
      error: (err) => {
        console.error('Error fetching trades', err);
        this.connected.set(false);
      }
    });
  }

  fetchStrategy() {
    if (!this.connected()) return;
    this.tradeService.getStrategy().subscribe({
        next: (data) => this.currentStrategy.set(data.name),
        error: (err) => console.error('Error fetching strategy', err)
    });
  }

  applyConfig() {
    this.tradeService.updateStrategy(this.selectedStrategy, this.selectedDecorator)
        .subscribe({
            next: (res) => {
                this.currentStrategy.set(res.name);
                alert('Strategy updated successfully!');
            },
            error: (err) => alert('Failed to update strategy')
        });
  }
}
