import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {CapitalizeEnumPipe} from "./pipes/capitalize-enum.pipe";



@NgModule({
  declarations: [CapitalizeEnumPipe],
  imports: [CommonModule],
  exports: [CapitalizeEnumPipe]
})
export class SharedModule { }
