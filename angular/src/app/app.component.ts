import { Component, OnDestroy, OnInit} from '@angular/core';
import { interval } from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import axios from 'axios';
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'web';
  intervalo = null;
  location = location.pathname
  atendidas = 0;
  atendendo = 0;
  porta = 0;
  uptime = 0;

  ngOnInit() {
    this.intervalo = setInterval(() => {
      axios.get('http://localhost:9090/virtual/telemetria', { headers: { 'Authorization': 'Basic ' + btoa('admin:admin')}})
        .then(res => {
          this.uptime = res.data.uptime;
          this.porta = res.data.porta;
          this.atendidas = res.data.atendidas;
          this.atendendo = res.data.atendendo;
      });
    }, 2000);
  }

  ngOnDestroy() {
    clearInterval(this.intervalo);
  }
}
