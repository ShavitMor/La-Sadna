import { ProductOrderModel } from "./ProductOrderModel";

export interface OrderModel {
    id: string;
    date: string;
    total: number;
    products: ProductOrderModel[];
}