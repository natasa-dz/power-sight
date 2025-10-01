import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'capitalizeEnum' })
export class CapitalizeEnumPipe implements PipeTransform {
  transform(value: any): string {
    if (!value) return '';
    return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase();
  }
}
