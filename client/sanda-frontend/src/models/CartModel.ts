import ProductCartModel from "./ProductCartModel";
export default interface cartModel{
    productsData: ProductCartModel[],
    oldPrice: number
    newPrice: number
}