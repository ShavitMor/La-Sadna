export default interface ProductModel {
    productID: number,
    productName: string,
    productPrice: number,
    productWeight: number,
    productCategory: string,
    description: string,
    productRank?: number
    storeId?: number
}