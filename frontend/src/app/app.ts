import { Component, OnInit, signal, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TradeService, Trade } from './services/trade.service';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, AfterViewInit {
  protected readonly title = signal('AI Trading Bot Dashboard');
  trades: Trade[] = [];
  connected = signal(true);

  currentStrategy = signal('Loading...');

  // Form State
  strategies = ['SmaCrossover', 'TrendFollowing', 'RSI', 'MACD', 'ADX', 'Default'];
  decorators = ['None', 'CrashProtection', 'HighRisk', 'LowRisk'];

  selectedStrategy = 'SmaCrossover';
  selectedDecorator = 'None';

  // Portfolio Metrics
  portfolioValue = signal(0);
  usdtBalance = signal(0);
  btcBalance = signal(0);

  // Chart
  @ViewChild('priceChart') priceChart!: ElementRef;
  chart: any;

  constructor(private tradeService: TradeService) {}

  ngOnInit() {
    this.fetchTrades();
    this.fetchStrategy();
    setInterval(() => this.fetchTrades(), 2000);
    setInterval(() => this.fetchStrategy(), 5000);
  }

  ngAfterViewInit() {
    this.createChart();
  }

  createChart() {
    const ctx = this.priceChart.nativeElement.getContext('2d');
    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          {
            label: 'BTC Price (USDT)',
            data: [],
            borderColor: '#4f46e5',
            backgroundColor: 'rgba(79, 70, 229, 0.1)',
            borderWidth: 2,
            pointRadius: 0,
            fill: true,
            tension: 0.4,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
        },
        scales: {
          x: { display: false },
          y: {
            position: 'right',
            grid: { color: '#e2e8f0' },
          },
        },
        animation: { duration: 0 },
      },
    });
  }

  fetchTrades() {
    this.tradeService.getTrades().subscribe({
      next: (data) => {
        if (data.length > 0) {
          const latest = data[data.length - 1];
          this.usdtBalance.set(latest.usdt || 1000);
          this.btcBalance.set(latest.btc || 0);

          const price = latest.price || 0;
          this.portfolioValue.set(this.usdtBalance() + this.btcBalance() * price);

          // Update Chart
          if (this.chart) {
            // Take last 50 points for better visualization
            const recentData = data.slice(-50);
            this.chart.data.labels = recentData.map((t) =>
              new Date(t.timestamp).toLocaleTimeString()
            );
            this.chart.data.datasets[0].data = recentData.map((t) => t.price);
            this.chart.update();
          }
        }

        this.trades = data.filter((t) => t.side !== 'HOLD').reverse();
        this.connected.set(true);
      },
      error: (err) => {
        console.error('Error fetching trades', err);
        this.connected.set(false);
      },
    });
  }

  fetchStrategy() {
    if (!this.connected()) return;
    this.tradeService.getStrategy().subscribe({
      next: (data) => {
        const name = data.name;
        this.currentStrategy.set(name);

        if (name.includes('ADX')) this.selectedStrategy = 'ADX';
        else if (name.includes('MACD')) this.selectedStrategy = 'MACD';
        else if (name.includes('RSI')) this.selectedStrategy = 'RSI';
        else if (name.includes('SMA') || name.includes('Sma'))
          this.selectedStrategy = 'SmaCrossover';
        else if (name.includes('Trend')) this.selectedStrategy = 'TrendFollowing';
        else if (name.includes('Risk') || name.includes('Default'))
          this.selectedStrategy = 'Default';
      },
      error: (err) => console.error('Error fetching strategy', err),
    });
  }

  applyConfig() {
    this.tradeService.updateStrategy(this.selectedStrategy, this.selectedDecorator).subscribe({
      next: (res) => {
        this.currentStrategy.set(res.name);
        alert('Strategy updated successfully!');
      },
      error: (err) => alert('Failed to update strategy'),
    });
  }
}
