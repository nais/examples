import React, { useState } from 'react'
import loadStuffFromApi from '../../io'
import './DataRetrieval.css'

const DataRetrieval = () => {
    const [myData, setMyData] = useState("initial state")

    const getData = () => {
        loadStuffFromApi()
            .then(stuff => {
                setMyData(`from api @ ${Date.now()}: ${stuff.data}`)
            })
            .catch(err => {
                setMyData(`${err}`)
            })
    }

    return (
        <div className='centered'>
            <button onClick = { () => getData() }>Hent ting fra API</button>
            <p>{ myData }</p>
        </div>
    )
}

export default DataRetrieval