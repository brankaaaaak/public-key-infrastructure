import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NgForOf, NgIf, NgTemplateOutlet} from '@angular/common';
import {CertificateService} from '../../services/certificate.service';
interface CertificateTreeDTO {
  serialNumber: string;
  type: string;
  ownerEmail: string;
  revoked: boolean;
  children: CertificateTreeDTO[];
}
@Component({
  selector: 'app-ca-view-certificate-chain',
  standalone: true,
  imports: [
    NgTemplateOutlet,
    NgForOf,
    NgIf
  ],
  templateUrl: './ca-view-certificate-chain.component.html',
  styleUrl: './ca-view-certificate-chain.component.scss'
})
export class ViewCertificateChainComponent implements OnInit {

  tree?: CertificateTreeDTO;

  constructor(private certificateService: CertificateService) {}


  ngOnInit(): void {
    this.certificateService.getMyCertificateChain()
      .subscribe({
        next: (data) => {
          this.tree = data;
          console.log("Chain podaci:", data);
        },
        error: (err) => {
          console.error("Greška pri povlačenju chain-a:", err);
          alert(err.error?.message || "Došlo je do greške.");
        }
      });
  }
}
