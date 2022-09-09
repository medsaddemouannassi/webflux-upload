import {Component, NgZone, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, Subject} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'test-upload-swf';
  url: string = 'http://localhost:8080/api'
  products!: any[];
  selectedFile!: any;
  error!: string;

  constructor(private http: HttpClient,
              private zone: NgZone) {
  }

  ngOnInit(): void {
    this.products = [];
    this.getProducts();
  }



  getProducts() {
    return new Observable(observer => {
      new EventSource(`${this.url}/products`).onmessage = event => {
        this.zone.run(() => {
          observer.next(JSON.parse(event.data))
        })
      };
    }).subscribe(data => {
        this.products = [...this.products, data];
    })
  }

  onFileSelected(any: any) {
    this.selectedFile = any.target.files[0];
  }

  add() {
    this.products = [];
    let formData = new FormData();
    if(this.selectedFile == undefined) {
      this.error = 'Please select file';
    } else {
      formData.append('name', 'pr' + Math.random())
      formData.append('price', '7.7')
      formData.append('image', this.selectedFile)
      this.http.post<any>(`${this.url}/products`, formData).subscribe(() => {
        this.getProducts();
      })
    }
  }

  delete(id: number) {
    this.products = [];
    this.http.delete<any>(`${this.url}/products/${id}`).subscribe(() => {
      this.getProducts();
    })
  }
}
