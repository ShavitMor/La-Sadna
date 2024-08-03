import React, { useContext, useEffect, useState } from 'react';
import { useForm, SubmitHandler } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import '../styles/CreateStore.css';
import { addProduct, createNewStore, editProduct, getProductDetails, hasPermission, loginUsingJwt } from '../API';
import { useNavigate, useParams } from 'react-router-dom';
import { AppContext } from '../App';
import Permission from '../models/Permission';
import { Categories } from '../models/CategoriesConst';
import ProductModel from '../models/ProductModel';
import RestResponse from '../models/RestResponse';

// Define the schema for validation
const schema = yup.object().shape({
  productName: yup.string().required('Product name is required'),
  productPrice: yup.number().positive('Product Price must be above positive').required('Product price is required'),
  category: yup.string().required('Product category is required'),
  productWeight: yup.number().positive('Product Weight must be above positive').required('Product weight is required'),
  productQuantity: yup.number().integer('Initial Amount must be whole').positive('Initial Amount must be positive').required('Product category is required'),
  description: yup.string()
});

const EditProduct = () => {
  const { productId } = useParams();
  const [product, setProduct] = useState<ProductModel>({ productID: 0, productPrice: 0, productName: "LOADING", productWeight: 0, productCategory: "LOADING", description: "LOADING"});
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, touchedFields }, reset,trigger } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      productName: product.productName,
      productPrice: product.productPrice,
      category: product.productCategory,
      productWeight: product.productWeight,
      description: product.description
    }
  });

  useEffect(() => {
    loadProduct();
}, [])

useEffect(() =>{
  reset({
    productName: product.productName,
    productPrice: product.productPrice,
    category: product.productCategory,
    productWeight: product.productWeight,
    description: product.description
  })
}, [product])

const loadProduct = async () => {
  var productResponse: RestResponse = await getProductDetails(parseInt(productId!));
  console.log(productResponse);
  if (!productResponse.error) {
      let productData: ProductModel = JSON.parse(productResponse.dataJson);
      setProduct(productData);
      let canAccess: boolean = await hasPermission(`${productData.storeId!}`, Permission.UPDATE_PRODUCTS);
        if(!canAccess){
            navigate('/permission-error', {state: "You do not have permission to edit products in the given store"})
        }
  } else {
      navigate('/permission-error', { state: productResponse.errorString });
  }
};

  const onSubmit: SubmitHandler<any> = async (data) => {
    const response = await editProduct(data, product.storeId!,product.productID);
    if (response.error) {
      alert("Error: " + response.errorString);
    } else {
      alert("Product edited successfully!");
      navigate(`/store/${product.storeId!}`);
    }
  };

  return (
    <div className="create-store-container">
      <h2>Edit Product</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="create-store-form">
        <div className="form-group">
          <label htmlFor="productName" className='label'>Product Name</label>
          <input
            id="productName"
            placeholder='Enter product name'
            {...register('productName', { onChange: () => trigger("productName") })}
            className={errors.productName && touchedFields.productName ? 'invalid' : ''}
          />
          {errors.productName && <p className="error-message">{errors.productName.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="productPrice" className='label'>Product Price</label>
          <input
            id="productPrice"
            placeholder='Enter product price'
            {...register('productPrice', { onChange: () => trigger("productPrice") })}
            className={errors.productPrice && touchedFields.productPrice ? 'invalid' : ''}
          />
          {errors.productPrice && <p className="error-message">{errors.productPrice.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="category" className='label'>Product Category</label>
          <input
            id="category"
            placeholder='Enter product category'
            {...register('category', { onChange: () => trigger("category") })}
            list="categs"
            className={errors.category && touchedFields.category ? 'invalid' : ''}
          />
          <datalist id="categs">
          {Categories.map(catego => <option>{catego}</option>)}
          </datalist>
          {errors.category && <p className="error-message">{errors.category.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="description" className='label'>Description</label>
          <input
            id="description"
            placeholder='Enter product description'
            {...register('description', { onChange: () => trigger("description") })}
            className={errors.description && touchedFields.description ? 'invalid' : ''}
          />
        </div>
        <div className="form-group">
          <label htmlFor="productWeight" className='label'>Product Weight</label>
          <input
            id="productWeight"
            placeholder='Enter product weight'
            {...register('productWeight', { onChange: () => trigger("productWeight") })}
            className={errors.productWeight && touchedFields.productWeight ? 'invalid' : ''}
          />
          {errors.productWeight && <p className="error-message">{errors.productWeight.message}</p>}
        </div>
        <div className="form-group">
          <label htmlFor="productQuantity" className='label'>Product Amount</label>
          <input
            id="productQuantity"
            placeholder='Enter amount'
            {...register('productQuantity', { onChange: () => trigger("productQuantity") })}
            className={errors.productQuantity && touchedFields.productQuantity ? 'invalid' : ''}
          />
          {errors.productQuantity && <p className="error-message">{errors.productQuantity.message}</p>}
        </div>
        <button type="submit" className='button'>Edit Product</button>
      </form>
    </div>
  );
};

export default EditProduct;
