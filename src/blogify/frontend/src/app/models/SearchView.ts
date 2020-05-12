export class SearchView<T> {

    constructor (
        public found: number,
        public hits: Hit<T>[],
        public page: number,
        public search_time_ms: number
    ) {}

}

class Hit<T> {

    constructor (
        public document: T
    ) {}

}
