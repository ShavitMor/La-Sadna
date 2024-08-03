export default interface AddProductModel {
    storeId?: number,
    productName: string,
    productId?: number,
    productQuantity: number;
    productPrice: number,
    category: string,
    rank?: number,
    productWeight: number,
    description: string
}