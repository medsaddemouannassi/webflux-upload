import {Component, NgZone, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

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
    this.getSyncProducts();
  }


  getStreamOfProducts() {
    return new Observable(observer => {
      const sse = new EventSource(`${this.url}/products/stream`)
      sse.onmessage = event => {
        this.zone.run(() => {
          observer.next(JSON.parse(event.data))
        })
      };
      sse.onerror = () => sse.close()
    }).subscribe(data => {
      this.products = [...this.products, data];
    })

  }

  getProducts() {
    return new Observable(observer => {
      const sse = new EventSource(`${this.url}/products`)
      sse.onmessage = event => {
        this.zone.run(() => {
          observer.next(JSON.parse(event.data))
        })
      };
      sse.onerror = () => sse.close()
    }).subscribe(data => {
      this.products = [...this.products, data];
    })

  }

  getSyncProducts(){
    return this.http.get<any>(`${this.url}/products`).subscribe(data => {
      console.log(data)
      this.products = data
    })
  }

  onFileSelected(any: any) {
    this.selectedFile = any.target.files[0];
    // @ts-ignore
    document.getElementById('fileName').innerHTML = any.target.files[0] != null ? any.target.files[0].name : '';
  }

  add() {
    this.products = [];
    let formData = new FormData();
    // @ts-ignore
    if (this.selectedFile == undefined || !document.getElementById('name').value || !document.getElementById('price').value) {
      if (this.selectedFile == undefined) {
        this.error = 'Please select file';
      }
      // @ts-ignore
      if (!document.getElementById('name').value) {
        this.error = 'Please enter title';
      }
      // @ts-ignore
      if (!document.getElementById('price').value) {
        this.error = 'Please enter price';
      }
    } else {
      // @ts-ignore
      formData.append('name', document.getElementById('name').value)
      // @ts-ignore
      formData.append('price', document.getElementById('price').value)
      formData.append('image', this.selectedFile)
      this.http.post<any>(`${this.url}/products`, formData).subscribe(() => {
        this.getSyncProducts();
      })
    }
  }

  delete(id: number) {
    this.products = [];
    this.http.delete<any>(`${this.url}/products/${id}`).subscribe(() => {
      this.getSyncProducts();
    })
  }
}
