import { CapitalizeEnumPipe } from './capitalize-enum.pipe';

describe('CapitalizeEnumPipe', () => {
  it('create an instance', () => {
    const pipe = new CapitalizeEnumPipe();
    expect(pipe).toBeTruthy();
  });
});
