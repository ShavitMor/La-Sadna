import React, { useContext, useEffect, useRef, useState } from 'react';
import ProductsBar from './ProductsBar';
import CategoriesBar from './CategoriesBar';
import { enterAsGuest, loginUsingJwt } from '../API';
import { AppContext } from '../App';
import { set } from 'date-fns';



const Home = () => {
    return (
        <div>
            <h1>Our top products</h1>
            <ProductsBar  />
            <h3>Our Top Categories</h3>
            <CategoriesBar />     
        </div>
    );
};

export default Home;