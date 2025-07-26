export interface PaymentSlipDTO {
  customerName: string;
  customerAddress: string;
  recipientName: string;
  recipientAccount: string;
  amount: number;
  purpose: string;
  model: number;
  referenceNumber: string;
}
