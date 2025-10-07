import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NgForOf, NgIf, NgTemplateOutlet} from '@angular/common';
import {CertificateService} from '../../services/certificate.service';
import {log} from '@angular-devkit/build-angular/src/builders/ssr-dev-server';
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

  tree?: CertificateTreeDTO[];
  selectedDetails?: any;
  constructor(private certificateService: CertificateService) {}


  ngOnInit(): void {
    this.certificateService.getMyCertificateChain()
      .subscribe({
        next: (data) => {
          this.tree = data; // sad je niz
          console.log("Chains:", data);
        },
        error: (err) => {
          console.error("Error:", err);
          alert(err.error?.message || "Error.");
        }
      });
  }



  selectNode(node: any, event: MouseEvent): void {
    event.stopPropagation();

    console.log("Selected certificate:", node.serialNumber);

    this.certificateService.getCertificateDetails(node.serialNumber)
      .subscribe({
        next: (data) => {
          this.selectedDetails = data;
          console.log("Details:", data);
        },
        error: (err) => {
          console.error(err);
          alert("You do not have access to this certificate")
        }
      });
  }




}
